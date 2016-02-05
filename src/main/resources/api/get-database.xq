declare option output:method "basex";

(: test :)
(:~ Path to resource. :)
declare variable $PATH as xs:string external;

let $db := if(contains($PATH, '/')) then substring-before($PATH, '/') else $PATH
let $path := substring-after($PATH, '/')
let $exists := db:exists($db, $path)
return if($exists and db:is-xml($db, $path)) then (

    let $doctype := if(contains($path, 'ditamap')) then
        (<output:doctype-public value="-//OASIS//DTD DITA Map//EN"/>, <output:doctype-system value="http://docs.oasis-open.org/dita/v1.1/OS/dtd/map.dtd"/>)
                    else if(contains($path, 'c_')) then
            (<output:doctype-public value="-//OASIS//DTD DITA Concept//EN"/>, <output:doctype-system value="http://docs.oasis-open.org/dita/v1.1/OS/dtd/concept.dtd"/>)
                    else if(contains($path, 'r_')) then
                (<output:doctype-public value="-//OASIS//DTD DITA Reference//EN"/>, <output:doctype-system value="http://docs.oasis-open.org/dita/v1.1/OS/dtd/reference.dtd"/>)
                    else if(contains($path, 't_')) then
                    (<output:doctype-public value="-//OASIS//DTD DITA Task//EN"/>, <output:doctype-system value="http://docs.oasis-open.org/dita/v1.1/OS/dtd/task.dtd"/>)
                    else ()

    let $params := <output:serialization-parameters>
                    <output:omit-xml-declaration value="no"/>
                    { $doctype }
                </output:serialization-parameters>
    return serialize(db:open($db, $path), $params)
) else if($exists and db:is-raw($db, $path)) then (
    db:retrieve($db, $path)
) else (
(: raise error if database or resource does not exist :)
error(xs:QName("api"), "Database resource does not exist: " || $PATH)
)
