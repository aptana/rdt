#!/usr/bin/ruby

name = "encapsulate_field_test_"
max = 0
Dir.new(".").each do |file| 
  if file =~ /encapsulate_field_test_(\d+)/
    max = $1.to_i if $1.to_i > max
  end
end

name = name + (max + 1).to_s
puts name

`touch #{name}.test_source`
`touch #{name}.test_result`

property = <<EOF
cursorPosition=
#the following properties are optional
#possible visibilites: public, protected, private
readerVisibility=
writerVisibility=
#values: true or false
enableReaderGeneration=
enableWriterGeneration=
EOF

`echo "#{property}" > #{name}.test_properties`


