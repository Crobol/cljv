(def ^:private translate-options [["-l" "--language LANG" "Language"]])

(defn- build-url [opts]
  (str "http://www.tyda.se/search/"
       (url-encode (first (:arguments opts)))))
       ;(if (contains? (:options opts) :language)
       ;  (cond
       ;   (= (get-in opts [:options :language]) "en") "?lang[0]=sv&lang[1]=en"
       ;   (= (get-in opts [:options :language]) "sv") "?lang[0]=en&lang[1]=sv"))))

(defn translate
  "Swedish/english dictionary through tyda.se"
  {:aliases ["t" "tyda"]}
  [query message]
  (when (not (clojure.string/blank? query))
    (let [opts (cli/parse-opts (clojure.string/split query #"\s") translate-options)
          url (build-url opts)
          raw-html (:body (http/get url))
          html (as-hickory (parse raw-html))
          result (s/select
                  (s/descendant
                  (s/and (s/tag :ul) (s/class "list") (s/class "list-translations"))
                  (s/and (s/tag :li) (s/class "item"))
                  (s/tag :a))
                  html)]
      (clojure.string/join ", " (take 4 (filter string? (map #(first (:content %)) result)))))
    )
  )
