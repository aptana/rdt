#!/usr/bin/ruby

name = "temp_to_field_test_"
max = 0
Dir.new(".").each do |file| 
  if file =~ /temp_to_field_test_(\d+)/
    max = $1.to_i if $1.to_i > max
  end
end
name = name + (max + 1).to_s
puts name

`touch #{name}.test_source`
`touch #{name}.test_result`

property = <<EOF
newName=
# isClassField = true oder false
isClassField=
#TempToFieldConverter.INIT_IN_METHOD oder TempToFieldConverter.INIT_IN_CONSTRUCTOR
initPlace=
cursorPosition=
EOF

`echo "#{property}" > #{name}.test_properties`