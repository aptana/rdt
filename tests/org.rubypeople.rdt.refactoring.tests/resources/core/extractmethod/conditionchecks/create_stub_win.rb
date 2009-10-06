#!/usr/bin/ruby
Dir.chdir(File.dirname(__FILE__));

name = "extract_method_checker_test_"
max = 0
Dir.new(".").each do |file| 
  if file =~ /extract_method_checker_test_(\d+)/
    max = $1.to_i if $1.to_i > max
  end
end

name = name + (max + 1).to_s
puts name

File.open("#{name}.test_source", File::CREAT) { |file| }

property = <<EOF
newMethodName=
start=
end=
initialError0=
initialError1=
#....
initialWarning0=
initialWarning1=
#...
finalError0=
finalError1=
#...
finalWarning0=
finalWarning1=
#...
EOF

File.open("#{name}.test_properties", File::CREAT|File::RDWR){ |file| file.syswrite "#{property}"}

