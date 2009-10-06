class ReportListView < Qt::ListView
  
  slots 'context_menu( QListViewItem *, const QPoint &, int)', 'remove_report()'
  signals 'active_report_removed()'
  attr_reader :selected_report

  def initialize(parent, name)
    super
    @popup_menu = Qt::PopupMenu.new
    @popup_menu.insert_item("Remove This Report", self, SLOT('remove_report()'))
    connect(self, SIGNAL('contextMenuRequested( QListViewItem *, const QPoint& , int )' ),
            self, SLOT('context_menu( QListViewItem *, const QPoint &, int)'));
  end
end