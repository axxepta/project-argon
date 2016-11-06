(:~ Path to new directory. :)
declare variable $PATH as xs:string external;

let $repopath := if (empty(db:system()//repopath)) then (
    error(xs:QName("api"), "Need admin rights to access repo path.")
) else (db:system()//repopath)

let $path := $repopath || '/' || $PATH
let $dir-exists := file:exists($path)
return if($dir-exists) then () else(file:create-dir($path))