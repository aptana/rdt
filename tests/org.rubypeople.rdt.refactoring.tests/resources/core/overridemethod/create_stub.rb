#!/usr/bin/ruby

name = "override_method_test_"
max = 0
Dir.new(".").each do |file| 
  if file =~ /override_method_test_(\d+)/
    max = $1.to_i if $1.to_i > max
  end
end

name = name + (max + 1).to_s
puts name

`touch #{name}.test_source`
`touch #{name}.test_super_source.rb`
`touch #{name}.test_result`

property = <<EOF
superclassfilename=#{name}.test_super_source.rb
#Format: ClassName[, AttributeName]
selection0=
selection1=
#......
EOF

`echo "#{property}" > #{name}.test_properties`


