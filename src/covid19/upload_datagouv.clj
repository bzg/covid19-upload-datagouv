(ns covid19.upload-datagouv
  (:require  [babashka.curl :as curl]
             [java-time :as t]
             [clojure.string :as s])
  (:gen-class))

(def datagouv-api "https://www.data.gouv.fr/api/1")

(def datagouv-api-token (System/getenv "DATAGOUV_API_TOKEN"))

(def datagouv-covid19-dataset "")

(def datagouv-endpoint-format
  (str datagouv-api
       "/datasets/"datagouv-covid19-dataset
       "/resources/%s/upload/"))

(def datagouv-api-headers
  {:headers {"Accept"    "application/json"
             "X-Api-Key" datagouv-api-token}})

(def spf-files-urls
  #{"https://url/file1.csv"
    "https://url/file2.csv"
    "https://url/file3.csv"})

(def datagouv-covid19-resources 
  {"file1.csv" {:resource ""}
   "file2.csv" {:resource ""}
   "file3.csv" {:resource ""}})

(defn get-filename-from-url [url]
  (s/replace url #"^.+/([^/]+)$" "$1"))

(defn download-spf-files [files]
  (println "Downloading SPF files...")
  (doseq [f files]
    (let [fn (get-filename-from-url f)]
      (curl/get f {:raw-args ["--output" fn]})))
  (println "Downloading SPF files... done"))

(defn upload-spf-files [files]
  (println "Uploading SPF files...")
  (doseq [f files]
    (let [resource (get-in datagouv-covid19-resources [f :resource])]
      (curl/post
       (format datagouv-endpoint-format resource)
       (merge datagouv-api-headers
              {:form-params {"file" (str "@" f)}}))))
  (println "Uploading SPF files... done"))

(defn -main []
  (download-spf-files spf-files-urls)
  (upload-spf-files spf-files-urls))
