class ModulTest
  include OuterModule::Module::InnerModule
  include Module::InnerModule
  include InnerModule
end