declare option output:method "raw";

(:~ Path to resource. :)
declare variable $PATH as xs:string external;

let $path := db:system()//repopath || '/' || $PATH

return if(file:exists($path)) then (
    file:read-binary($path)
) else (
(: raise error if resource does not exist :)
error(xs:QName("api"), "Repository resource does not exist: " || $PATH)
)
