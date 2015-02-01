(def ^:private ud-url-template "http://api.urbandictionary.com/v0/define?term=%s")

(defn urban-dictionary
  "Query Urban Dictionary"
  {:aliases ["ud"]}
  [query message]
  (let [url (format ud-url-template (url-encode query))
        raw-json (:body (http/get url))
        json (json/read-str raw-json)
        definition (get (first (get json "list")) "definition")]
    (when (some? definition)
      (clojure.string/replace definition #"\s+" " "))))
