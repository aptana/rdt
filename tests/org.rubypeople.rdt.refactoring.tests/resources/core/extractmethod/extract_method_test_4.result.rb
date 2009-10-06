class ReportListView < Qt::ListView

  def remove_report selected_item
    return unless selected_item
    @selected_report = selected_item
    remove_item selected_item
  end

  def remove_item selected_item
    take_item selected_item
    emit active_report_removed
  end
  protected :remove_item

end