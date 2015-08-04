(def ^:private wikipedia-options [["-l" "--language" :default "en"]])

(defn wikipedia
  "Query Wikipedia for info"
  {:aliases ["w"] :options wikipedia-options}
  [query message]
  (run-query (bot.providers.wikipedia.Wikipedia-provider.) query))
