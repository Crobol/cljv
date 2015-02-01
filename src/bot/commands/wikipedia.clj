(def ^:private url-template "https://%s.wikipedia.org/w/api.php?action=query&prop=info|extracts&inprop=url&format=json&exchars=400&explaintext&redirects&titles=%s")

(def ^:private wikipedia-options [["-l" "--language" :default "en"]])

(defn wikipedia
  "Query Wikipedia for info"
  {:aliases ["w"] :options wikipedia-options}
  [query message]
  (run-query (bot.providers.wikipedia.Wikipedia-provider.) query))
