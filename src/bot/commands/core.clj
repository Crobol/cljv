(ns bot.commands.core
  [:use [bot.commands]])

(defn get-meta
  ([command]
    (meta (resolve (symbol command))))
  ([ns command]
    (meta (ns-resolve ns (symbol command))))
  )

(defn filter-starts-with [s coll]
  (filter #(.startsWith (first %) s) coll))

(defn get-commands []
  (vals (ns-publics 'bot.commands)))

(defn get-command-names [command]
  (let [command-meta (meta command)]
    (if (contains? command-meta :aliases)
      (conj (:aliases command-meta) (str (:name command-meta)))
      [(str (:name command-meta))])))

(defn search-commands [query]
  (filter (fn [c]
            (let [names (get-command-names c)]
              (some #(.startsWith % query) names))) (get-commands)))

(defn get-command [command-name]
  (first (filter (fn [c]
                   (let [names (get-command-names c)]
                     (some #(= % command-name) names)))
                 (get-commands))))

(defn auto-complete-command [query]
  (if-let [command (get-command query)]
    [command]
    (search-commands query)))
