#!/usr/bin/ruby

name = "extract_method_test_"
max = 0
Dir.new(".").each do |file| 
  if file =~ /extract_method_test_(\d+)/
    max = $1.to_i if $1.to_i > max
  end
end

name = name + (max + 1).to_s
puts name

`touch #{name}.result.rb`
`touch #{name}.source.rb`

property = <<EOF
start=
end=
name=
visibility=private
# the next two properties are optional
# specify the operations like you would perform them in the GUI, for example: 1 up or 0 down
# speficy multiple operations comma-separated.
order=
# the names are changed after the items are re-arranged
names=
EOF

`echo "#{property}" > #{name}.test_properties`


