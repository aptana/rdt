#!/usr/bin/ruby

name = "push_down_test_"
max = 0
Dir.new(".").each do |file| 
  if file =~ /push_down_test_(\d+)/
    max = $1.to_i if $1.to_i > max
  end
end

name = name + (max + 1).to_s
puts name

`touch #{name}.test_source`
`touch #{name}.test_project_source_0.rb`
`touch #{name}.test_project_source_1.rb`
`touch #{name}.test_result`

property = <<EOF
projectfilename0=#{name}.test_project_source_0.rb
projectfilename1=#{name}.test_project_source_1.rb
#Format: ClassName[, MethodName]
selection0=
selection1=
#......
EOF

`echo "#{property}" > #{name}.test_properties`


