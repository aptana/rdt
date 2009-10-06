[1, 2, 3, 4].each do |inner|
  puts inner
  ["a"].each do |inner, arg2|
    puts inner
  end
  p outer.class
end