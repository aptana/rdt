#!/usr/bin/ruby

name = "generate_constructor_"
max = 0
Dir.new(".").each do |file| 
  if file =~ /generate_constructor_(\d+)/
    max = $1.to_i if $1.to_i > max
  end
end

name = name + (max + 1).to_s
puts name

`touch #{name}.test_source`
`touch #{name}.test_result`

property = <<EOF
#Format: ClassName[, AttributeName]
selection0=
selection1=
#......
EOF

`echo "#{property}" > #{name}.test_properties`


