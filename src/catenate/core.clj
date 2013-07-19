(ns catenate.core)

(defn arity [f]
  (let [m (first (filter #(#{"invoke" "doInvoke"} (.getName %))
                         (.getDeclaredMethods (class f))))
        p (.getParameterTypes m)]
    (alength p)))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
