(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ New path to resource. :)
declare variable $NEWPATH as xs:string external;

let $path := db:system()//repopath || '/' || $PATH
let $newpath := db:system()//repopath || '/' || $NEWPATH

return file:move($path, $newpath)
