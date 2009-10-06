#!/usr/bin/ruby

name = "generate_accesssor_"
max = 0
Dir.new(".").each do |file| 
  if file =~ /generate_accesssor_(\d+)/
    max = $1.to_i if $1.to_i > max
  end
end

name = name + (max + 1).to_s
puts name

`touch #{name}.test_source`
`touch #{name}.test_result`

property = <<EOF
#TYPE_METHOD_ACCESSOR or TYPE_SIMPLE_ACCESSOR
type=
#Format: ClassName, AttributeName, readerSelected, writerSelected
#Types: ClassName and AttributeName -> String, readerSelected and writerSelected -> bool -> true or false
selection0=
selection1=
#......
EOF

`echo "#{property}" > #{name}.test_properties`


