(defn finance
  "Finance information"
  {:aliases ["f"]}
  [query message]
  (run-query (bot.providers.finance.Finance-provider.) query))
