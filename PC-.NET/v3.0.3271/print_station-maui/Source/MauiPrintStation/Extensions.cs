using System;
using System.Collections.Generic;
using System.Collections.ObjectModel;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ExtensionMethods
{
    public static class Extensions
    {
        public static void SafeClear<T>(this ObservableCollection<T> observableCollection)
        {
            if (!MainThread.IsMainThread)
            {
                MainThread.BeginInvokeOnMainThread(async () =>
                {
                    while (observableCollection.Any())
                    {
                        observableCollection.RemoveAt(0);
                    }
                });
            }
            else
            {
                while (observableCollection.Any())
                {
                    observableCollection.RemoveAt(0);
                }
            }
        }
        public static void SafeAdd<T>(this ObservableCollection<T> observableCollection, T item)
        {
            if (!MainThread.IsMainThread)
            {
                MainThread.BeginInvokeOnMainThread(async () =>
                {
                    if (!observableCollection.Contains(item))
                    {
                        observableCollection.Add(item);
                    }
                });
            }
            else
            {
                if (!observableCollection.Contains(item))
                {
                    observableCollection.Add(item);
                }
            }
        }
        public static void SafeAddAll<T>(this ObservableCollection<T> observableCollection, IEnumerable<T> items)
        {
            if (!MainThread.IsMainThread)
            {
                MainThread.BeginInvokeOnMainThread(async () =>
                {
                    foreach (var item in items)
                    {
                        if (!observableCollection.Contains(item))
                        {
                            observableCollection.Add(item);
                        }
                    }
                });
            }
            else
            {
                foreach (var item in items)
                {
                    if (!observableCollection.Contains(item))
                    {
                        observableCollection.Add(item);
                    }
                }
            }
        }
    }
}
