#!/usr/bin/ruby

name = "move_method_test_"
max = 0
Dir.new(".").each do |file| 
  if file =~ /move_method_test_(\d+)/
    max = $1.to_i if $1.to_i > max
  end
end

name = name + (max + 1).to_s
puts name

`touch #{name}.activeFile.rb.result`
`touch #{name}.activeFile.rb.source`

property = <<EOF
activeFile=activeFile.rb
caretPosition=
selectedClass=
# true false value
leaveDelegateMethod=
# Optional value
selectedField=
EOF

`echo "#{property}" > #{name}.test_properties`

