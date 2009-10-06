[1, 2, 3, 4].each do |outer|
  puts outer
  ["a"].each do |inner, arg2|
    puts inner
  end
  p outer.class
end