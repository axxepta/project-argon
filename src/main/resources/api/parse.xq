(:~ Path to resource. :)
declare variable $QUERY as xs:string external;

return xquery:parse($QUERY, map { 'plan': false() })
