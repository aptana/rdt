class ArticlesController < ApplicationController

  layout "standard"

  def index
    list
    render :action => 'list'
  end

  # GETs should be safe (see http://www.w3.org/2001/tag/doc/whenToUseGet.html)
  verify :method => :post, :only => [ :destroy, :create, :update ],
         :redirect_to => { :action => :list }

  def list
    @selected_order = get_selected_order(params)

    @articles = Article.find(:all, :conditions => ["order_id = ?", @selected_order.id])
    @articles = @articles.sort_by {|a| a.person.name }
  end

  def new
    @article = Article.new
  end

  def create
    @article = Article.new(params[:article])
    if @article.save
      flash[:notice] = 'Article was successfully created.'
      redirect_to :action => 'list', :id => @article.order_id
    else
      render :action => 'new', :id => @article.order_id
    end
  end

  def edit
    @article = Article.find(params[:id])
  end

  def update
    @article = Article.find(params[:id])
    if @article.update_attributes(params[:article])
      flash[:notice] = 'Article was successfully updated.'
      redirect_to :action => 'list', :id => @article.order_id
    else
      render :action => 'edit'
    end
  end

  def destroy
    article = Article.find(params[:id])
    article.destroy
    redirect_to :action => 'list', :id => article.order_id
  end
end
