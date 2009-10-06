def methode
  replace_br
  replace(doc, 'img') {|img| img.attributes['alt'] || ""}
end

def replace_br
  replace(doc, 'br') { |br| "\n" }
end
