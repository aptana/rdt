# Filters added to this controller will be run for all controllers in the application.
# Likewise, all the methods added will be available for all controllers.
class ApplicationController < ActionController::Base

  private
  def get_selected_order(params)
    
    if params.has_key? :selected_order
      id = params[:selected_order][:id]
    else
      id = params[:id]
    end
    
    begin
      return Order.find(id)
    rescue ActiveRecord::RecordNotFound
      return Order.find(:first)
    end  
  end

end