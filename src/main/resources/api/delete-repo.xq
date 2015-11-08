(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ Resource. :)
declare variable $RESOURCE as xs:base64Binary external;

let $path := db:system()//repopath || '/' || $PATH
return file:delete($path)