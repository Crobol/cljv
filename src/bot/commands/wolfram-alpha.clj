(def ^:private appid (env/env :wolfram-app-id))

(defn wolfram-alpha
  "Query Wolfram-Alpha"
  {:aliases ["a" "wa"]}
  [query message]
  (run-query (bot.providers.wolfram_alpha.Wolfram-alpha-provider.) query))
