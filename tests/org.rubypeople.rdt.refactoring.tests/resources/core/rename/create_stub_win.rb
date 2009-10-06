#!/usr/bin/ruby
Dir.chdir(File.dirname(__FILE__));
name = "rename_test_"
max = 0
Dir.new(".").each do |file| 
  if file =~ /#{name}(\d+)/
    max = $1.to_i if $1.to_i > max
  end
end

name = name + (max + 1).to_s
puts name

File.open("#{name}.source.rb", File::CREAT) { |file| }

property = <<EOF
cursorPosition=
#possible values: none, renameClass, renameMethod, renameField, renameLocal
delegateRenameRefactoring=
EOF

File.open("#{name}.test_properties", File::CREAT|File::RDWR){ |file| file.syswrite "#{property}"}
