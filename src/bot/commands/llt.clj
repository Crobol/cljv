(defn llt
  "Query LLT for trip schedule"
  {:aliases ["bus"]}
  [query message]
  (if (clojure.string/blank? query)
    (help "llt" message)
    (run-query (bot.providers.llt.Llt-provider.) query))
  )
