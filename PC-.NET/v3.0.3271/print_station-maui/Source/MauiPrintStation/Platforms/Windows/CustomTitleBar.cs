using Microsoft.Maui;
using Xaml = Microsoft.UI.Xaml;
using XamlControls = Microsoft.UI.Xaml.Controls;

namespace MauiPrintStation.WinUI;

internal class CustomTitleBar : XamlControls.StackPanel
{
	public CustomTitleBar()
	{
        Background = new Xaml.Media.SolidColorBrush(Microsoft.UI.Colors.Transparent);

        var grid = new XamlControls.Grid();

        grid.Background = new Xaml.Media.SolidColorBrush(Microsoft.UI.Colors.Transparent);

        grid.Name = "CustomTitleBar";


        grid.RowDefinitions.Add(new XamlControls.RowDefinition()
        {
            Height = new Xaml.GridLength(1, Xaml.GridUnitType.Star)
        });

        grid.ColumnDefinitions.Add(new XamlControls.ColumnDefinition() 
        { 
            Width = new Xaml.GridLength(1, Xaml.GridUnitType.Star) 
        });

        var button = new XamlControls.Button()
        {
            Content = "This is a button"
        };

        grid.Children.Add(button);

        XamlControls.Grid.SetColumn(button, 0);
        XamlControls.Grid.SetRow(button, 0);

        var border = new XamlControls.Border();

        border.Child = grid;

        Children.Add(border);
    }
}
