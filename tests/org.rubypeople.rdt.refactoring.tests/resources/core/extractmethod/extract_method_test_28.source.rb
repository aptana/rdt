def methode
  replace(doc, 'br')  {|br| "\n"}
  replace(doc, 'img') {|img| img.attributes['alt'] || ""}
end
