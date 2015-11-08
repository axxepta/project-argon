(:~ Path to resource. :)
declare variable $PATH as xs:string external;

let $path := db:system()//repopath || '/' || $PATH
return xquery:parse-uri($path, map { 'plan': false() })