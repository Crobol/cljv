(ns bot.providers.wolfram-alpha
  (:require [clojure.tools.cli :as cli]
            [clj-http.client :as http]
            [environ.core :as env]
            [hickory.select :as s]
            [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.data.zip.xml :refer [text xml-> xml1-> attr attr=]])
  (:use [clj-http.util :only (url-encode)]
        [bot.query-provider]
        [bot.core :only (truncate-string)]))

(def ^:private appid (env/env :wolfram-app-id))

(defn- definition-formatter [answer]
  (take 3 (filter
           #(not (clojure.string/blank? %))
           (map #(clojure.string/trim %)
                (clojure.string/split answer #"\d \| ")))))

(defn- result-with-decimal-formatter [xml]
  (let [exact-answer (xml1-> xml :pod (attr= :primary "true") (attr= :id "Result") :subpod :plaintext text)
        decimal-approximation (xml1-> xml :pod (attr= :primary "true") (attr= :id "DecimalApproximation") :subpod :plaintext text)
        truncated-decimals (bot.core/truncate-string decimal-approximation 20 "...")]
    (str exact-answer " (" truncated-decimals ")")))

(defn- is-word-definition? [xml]
   (some? (xml1-> xml :pod (attr= :primary "true") (attr= :id "Definition:WordData"))))

(defn- has-decimal-approximation? [xml]
  (and
   (some? (xml1-> xml :pod (attr= :primary "true") (attr= :id "Result")))
   (some? (xml1-> xml :pod (attr= :primary "true") (attr= :id "DecimalApproximation")))))

(defn- has-primary? [xml]
  (some? (xml1-> xml :pod (attr= :primary "true") (attr :id))))

(defn- result-formatter [raw-xml]
  (let [stream (java.io.ByteArrayInputStream.
                (.getBytes raw-xml))
        xml (zip/xml-zip (xml/parse stream))
        pod (xml1-> xml :pod)
        answer (xml1-> xml :pod (attr= :primary "true") :subpod :plaintext text)]
    (cond
     (is-word-definition? xml) (definition-formatter answer)
     (has-decimal-approximation? xml) (result-with-decimal-formatter xml)
     (has-primary? xml) answer
     :else (xml1-> xml :pod :subpod :plaintext text))
    ))

(deftype Wolfram-alpha-provider []
  Query-provider
  (provider-name [this] "Wolfram-Alpha")
  (run-query [this query]
             (let [raw-xml (:body (http/get (str "https://api.wolframalpha.com/v2/query?appid=" appid "&input=" (url-encode query) "&format=plaintext&async=false&reinterpret=true")))]
               (result-formatter raw-xml)
               )))
