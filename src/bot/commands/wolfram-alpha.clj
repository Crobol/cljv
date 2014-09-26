(def ^:private appid (env/env :wolfram-app-id))

(defn wolfram-alpha
  "Query Wolfram-Alpha"
  {:aliases ["a" "wa"]}
  [query message]
  (let [raw-xml (:body (http/get (str "http://api.wolframalpha.com/v2/query?appid=" appid "&input=" (url-encode query) "&format=plaintext&async=false&reinterpret=true")))
        stream (java.io.ByteArrayInputStream.
	                        (.getBytes raw-xml))
        xml (zip/xml-zip (xml/parse stream))
        title (xml1-> xml :pod (attr= :primary "true") (attr :title))
        answer (xml1-> xml :pod (attr= :primary "true") :subpod :plaintext text)]
    (if (not (clojure.string/blank? answer))
     (str title ": " answer))
    ))
