def test arg1, arg2 = 5, *arg3, &blockarg
  puts arg1 + arg2 - arg3 * blockarg
end