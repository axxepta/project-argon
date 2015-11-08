(:~ Path to resource. :)
declare variable $PATH as xs:string external;
(:~ Resource (XML string or Base64). :)
declare variable $RESOURCE as xs:string external;

let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
let $path := substring-after($PATH, '/')

return if(starts-with($RESOURCE, '<')) then (
(: first byte is angle bracket :)
let $xml := try {
    parse-xml($RESOURCE)
} catch * {
(: raise error if input is not well-formed :)
error(xs:QName("api"), "Resource is not well-formed")
}
(: return db:add($db, $xml, $path) :)
return db:replace($db, $path, $xml)
) else (
    db:store($db, $path, xs:base64Binary($RESOURCE))
)