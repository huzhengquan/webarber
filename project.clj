(defproject webarber "0.1.2"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [factual/clj-leveldb "0.1.0"]
                 [org.clojure/data.json "0.2.5"]
                 [barber "0.1.6"]]
  :plugins [[lein-ring "0.8.10"]]
  :ring {:handler webarber.handler/app}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}}
  :jvm-opts ["-Dlevel.url.path=db/url" "-Dlevel.page.path=db/page"])
