(ns bot.query-provider)

(defprotocol Query-provider
  (provider-name [this] "Name of the provider")
  (options [this] "Option specification of the provider")
  (run-query [this query] "Query this provider"))
