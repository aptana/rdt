def ddd
  [1, 2, 3].each do |i|
    puts i
    retry
  end
end

ddd