(ns catenate.core)

(defn wrap [app & {:keys [env bundles context-path]}]
  (fn [request]
    (app request)))
