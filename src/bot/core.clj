(ns bot.core
  [:require [clj-http.client :as http]]
  [:import [org.apache.commons.lang StringEscapeUtils]])

(defn get-urls
  ([line] (get-urls line [#"https?://\S*"]))
  ([line url-regexes]
    (filter
      #(not (nil? %))
      (flatten
        (map #(re-seq % line) url-regexes)))))

(defn get-title [url]
  (when-let [html (:body (http/get url {:headers {"User-agent" "Mozilla/5.0 (Windows NT 6.1;) Gecko/20100101 Firefox/13.0.1"}}))]
    (org.apache.commons.lang.StringEscapeUtils/unescapeHtml
      (second (re-find #"(?is)<title>(.*)</title>" html)))))

(defn url-titles
  ([line] (url-titles line [#"https?://\S*"]))
  ([line url-regexes]
    (map get-title (get-urls line url-regexes))))

(defn is-valid-string-result [result]
  (and (string? result) (not (clojure.string/blank? result))))

(defn is-valid-coll-result [result]
  (coll? result))
