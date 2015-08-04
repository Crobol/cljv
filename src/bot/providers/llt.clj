(ns bot.providers.llt
  (:require [clj-http.client :as http]
            [hickory.select :as s]
            [environ.core :as env]
            [clojure.zip :as zip]
            [clojure.xml :as xml]
            [clojure.data.zip.xml :refer [text xml-> xml1-> attr attr=]]
            [clj-time.core]
            [clj-time.local]
            [clj-time.format]
            [clojure.tools.logging :as log])
  (:use [hickory.core :only (parse as-hickory)]
        [clj-http.util :only (url-encode)]
        [bot.query-provider]))

(def ^:private hours-minutes-formatter (clj-time.format/formatter (clj-time.core/default-time-zone) "HH:mm" "HH:mm"))
(def ^:private days-formatter (clj-time.format/formatter (clj-time.core/default-time-zone) "d" "d"))
(def ^:private months-formatter (clj-time.format/formatter (clj-time.core/default-time-zone) "M" "M"))
(def ^:private years-formatter (clj-time.format/formatter (clj-time.core/default-time-zone) "yy" "yy"))
(def ^:private date-formatter (clj-time.format/formatter (clj-time.core/default-time-zone) "yyyy-MM-dd" "yyyy-MM-dd"))

(defn- extract-time [td-element]
  (if (= (get-in td-element [:content 0 :tag]) :em)
    (get-in td-element [:content 0 :content 0])
    (get-in td-element [:content 0])
    )
  )

(defn- extract-trip [tr-element]
  {:departure (extract-time (get-in tr-element [:content 3]))
   :arrival (extract-time (get-in tr-element [:content 5]))
   :travel-time (extract-time (get-in tr-element [:content 7]))
   :parts (map #(get-in % [:attrs :title]) (get-in tr-element [:content 11 :content]))
   }
  )

(defn- extract-trips [parsed-html-result]
  (let [results (s/select
                  (s/descendant
                    (s/and (s/tag :tr) (s/class :search-result))
                    )
                  parsed-html-result)]
    (map 
      (fn [x] (extract-trip x))
      results)
    )
  )

(defn- get-search-page-html [from to departure]
  (let [url "http://193.45.213.123/lulea/v2/querypage_adv.aspx"
        raw-html (:body (http/post url {:as :auto
                                        :headers 
                                        {"user-agent" "User-Agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.93 Safari/537.36"
                                         "accept-language" "Accept-Language:en-US,en;q=0.8,sv;q=0.6"
                                         "content-type" "application/x-www-form-urlencoded"
                                         "origin" "http://193.45.213.123"
                                         }
                                        :form-params {:inpPointFr from
                                                      :txtPointFr from
                                                      :inpPointTo to
                                                      :txtPointTo to
                                                      :selRegionRf "741"
                                                      :selDay (clj-time.format/unparse days-formatter departure)
                                                      :selMonth (clj-time.format/unparse months-formatter departure)
                                                      :selYear (clj-time.format/unparse years-formatter departure)
                                                      :inpTime (clj-time.format/unparse hours-minutes-formatter departure)
                                                      :selDirection "0"
                                                      :selDirection2 "0"
                                                      :cmdAction "search"
                                                      :VerNo "1.2.0.19"
                                                      :RegionFrName "Norrbotten"
                                                      :RegionToName "Norrbotten"
                                                      :SupportsScript "False"
                                                      :BrowserType "NE4"
                                                      :Language "se"
                                                      }}))
        parsed-html (as-hickory (parse raw-html))
        ]
    parsed-html))

(defn- get-sel-point [parsed-html name]
  (let [select (s/select
                 (s/descendant
                   (s/and (s/tag :select) (s/attr :name #(= % name))))
                 parsed-html)]
    (get-in select [0 :content 1 :attrs :value])
    )
  )

(defn- get-trip-points [search-page-html]
  (let [fr (get-sel-point search-page-html "selPointFr")
        to (get-sel-point search-page-html "selPointTo")]
    {:from fr :to to}))

(defn- get-search-result-html [from to departure]
  (let [url "http://193.45.213.123/lulea/v2/resultspage.aspx"
        raw-html (:body (http/post url {:as :auto
                                        :headers 
                                        {"user-agent" "User-Agent:Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.93 Safari/537.36"
                                         "accept-language" "Accept-Language:en-US,en;q=0.8,sv;q=0.6"
                                         "content-type" "application/x-www-form-urlencoded"
                                         "cache-control" "max-age=0"
                                         "origin" "http://193.45.213.123"
                                         "Referer" "http://193.45.213.123/lulea/v2/querypage_adv.aspx"}
                                        :form-params {:inpPointFr from
                                                      :inpPointTo to
                                                      :selRegionRf "741"
                                                      :selDay (clj-time.format/unparse days-formatter departure)
                                                      :selMonth (clj-time.format/unparse months-formatter departure)
                                                      :selYear (clj-time.format/unparse years-formatter departure)
                                                      :inpTime (clj-time.format/unparse hours-minutes-formatter departure)
                                                      :inpDate (clj-time.format/unparse date-formatter departure)
                                                      :selPointFr from
                                                      :selPointTo to
                                                      :selDirection "0"
                                                      :selDirection2 "0"
                                                      :cmdAction "search"
                                                      :VerNo "7.1.1.2.0.38p3"
                                                      :RegionFrName "Norrbotten"
                                                      :RegionToName "Norrbotten"
                                                      :SupportsScript "False"
                                                      :BrowserType "NE4"
                                                      :Language "se"
                                                      }}))
        parsed-html (as-hickory (parse raw-html))]
    parsed-html))

(deftype Llt-provider []
  Query-provider
  (provider-name [this] "LLT")
  (run-query [this query]
    (when (not (clojure.string/blank? query))
      (let [split (clojure.string/split query #"\s")
            search-page-html (get-search-page-html (first split) (second split) (clj-time.local/local-now))
            trip-points (get-trip-points search-page-html)
            search-result (get-search-result-html (:from trip-points) (:to trip-points) (clj-time.local/local-now))
            trips (extract-trips search-result)
            from (first (clojure.string/split (:from trip-points) #"\|"))
            to (first (clojure.string/split (:to trip-points) #"\|"))]
        (concat
          [(str from " -> " to)]
          (map #(str 
                  (:departure %) " - " (:arrival %) 
                  (when (not-empty (:parts %)) " | ")
                  (clojure.string/join " -> " (:parts %)))
               (take 3 trips)))
        )
      )
    )
  )

