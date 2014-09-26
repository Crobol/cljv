(defn- options-summary [options]
  (clojure.string/join " "
                       (map #(str "[" (clojure.string/trim %) "]")
                            (clojure.string/split
                             (:summary (cli/parse-opts [] options))
                             #"\n"))))

(defn help
  "Show the help message for the specified command"
  [command-name message]
  (if (clojure.string/blank? command-name)
    (str "Available commands: " (clojure.string/join ", " (map #(:name (meta %)) (vals (ns-publics 'bot.commands)))))
    (if-let [command-meta (meta (ns-resolve 'bot.commands (symbol command-name)))]
      (str
       "." (:name command-meta)
       " "
       (when (contains? command-meta :options)
         (str (options-summary (:options command-meta)) " "))
       "<" (first (last (:arglists command-meta))) ">"
       " - "
       (:doc command-meta)
       )
      )
    )
  )
