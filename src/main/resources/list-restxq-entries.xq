(:~
 : This query returns all entries (directories and files)
 : of a RESTXQ directory.
 :)

(:~ Relative path. :)
declare variable $path external := '.';

let $system := db:system()
let $webpath := $system//webpath/string()
(: absolute, or relative to webpath :)
let $restxqpath := $system//restxqpath/string()

return element resources {
  (: build root directory. :)
  let $root := file:resolve-path($restxqpath, file:resolve-path($webpath)) || $path
  (: retrieve all entries on this and lower levels :)
  for $resource in file:children($root)
  let $name := file:name($resource)
  let $dir := file:is-dir($resource)
  (: show directories first, case-insensitive order :)
  order by $dir descending, lower-case($name)
  return element { if($dir) then 'directory' else 'file' } {
    attribute size { file:size($resource) },
    $name
  }
}
