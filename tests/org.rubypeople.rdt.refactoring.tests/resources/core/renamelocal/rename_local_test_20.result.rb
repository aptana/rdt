def test arg1, arg2 = 5, *var_params, &arg4
  puts arg1 + arg2 - var_params * arg4
end