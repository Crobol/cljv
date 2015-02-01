
(defn- format-result [provider-name query-result]
  (if (coll? query-result)
    (map #(str provider-name ": " %) query-result)
    (str provider-name ": " query-result)))

(defn- query-providers [query providers]
  (filter #(some? %)
          (map
           #(try (when-let [result (bot.query-provider/run-query % query)]
                   (format-result (bot.query-provider/provider-name %) result))
              (catch Exception e (log/error e)))
           providers)))

(def ^:private providers [(bot.providers.urban_dictionary.Urban-dictionary-provider.)
                          (bot.providers.wikipedia.Wikipedia-provider.)
                          (bot.providers.wolfram_alpha.Wolfram-alpha-provider.)])

(defn unified-search
  "Unified search"
  {:aliases ["us" "omni"] :options nil}
  [query message]
  (cond
   (bot.core/contains-url? query) (bot.core/url-titles query)
   :else (flatten (query-providers query providers))))
