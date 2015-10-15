
(:~ Path to resource. :)
declare variable $XQUERY as xs:string external;

xquery:parse($XQUERY, map { 'plan': false(), 'pass': true() })
