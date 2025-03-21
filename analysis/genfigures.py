
import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import matplotlib.lines as mlines


from s2soptions import rename

def rreplace(s, old, new, occurrence):
    li = s.rsplit(old, occurrence)
    return new.join(li)

def thousands_to_k(s):
    return rreplace(s, '000', 'K', 1)

def vargroupsize(df, outname, desired_range=(1, None)):

    df['mod'] = df['query'].apply(lambda q: int(q.split('_')[1]))
    df['size'] = df['query'].apply(lambda q: int(q.split('_')[2]))
    df['name'] = df['multi_threading'].apply(rename)
    df['query_name'] = df['query'].apply(lambda q: q.split('_')[0])

    if len(df['size'].unique()) > 1:
        raise ValueError('currently one size is supported ' + str(df['size'].unique()) + ' - ' + str(len(df['size'].unique())))

    names = df['query_name'].unique()
    for name in names:
        tmp_df = df[df['query_name'] == name]
    
        tmp_df = tmp_df[['name', 'mod', 'time', 'err']]
        tmp_df = tmp_df.sort_values(by=['name', 'mod'])

        plt.figure(figsize=(10, 8))

        # Define line styles using consistent tuple notation
        dashes = {'CG': (1, 1), 'CGCC': (3, 3), 'PU': (1, 0)}  

        sns.lineplot(data=tmp_df, 
                    x='mod', y='time', 
                    hue='name', 
                    linewidth=10,
                    style='name',
                    markers=False,
                    dashes=dashes) 

        start_x_tick, end_x_tick = desired_range
        ticks = plt.xticks()[0] 
        ticks = [(start_x_tick if t == 0 else t) for t in ticks if t >= 0]  
        if end_x_tick:
            while ticks[-1] > end_x_tick: 
                ticks = ticks[:-1]

        tick_labels = [thousands_to_k(str(int(t))) for t in ticks]
        ticksize = 28
        plt.xticks(ticks, labels=tick_labels, fontsize=ticksize, fontweight="bold") 
        plt.yticks(fontsize=ticksize, fontweight="bold")  

        plt.xlabel('')
        plt.ylabel('')
        plt.title('')
        plt.grid(True)

        # Apply log scale
        plt.yscale("log")

        palette = sns.color_palette()
        # --- Generate Legend Handles Correctly ---
        legend_handles = [
            mlines.Line2D([], [], color=palette[i], linestyle=(0, style), 
                        linewidth=5, label=name)
            for i, (name, style) in enumerate(dashes.items())
        ]

        # Remove legend from main plot
        plt.legend().remove()  

        # Save main plot
        plt.savefig(outname.replace('png', name+'.png'), dpi=300, bbox_inches='tight')

        # --- Create separate figure for legend ---
        fig_legend, ax_legend = plt.subplots(figsize=(10, 1))  # Horizontal layout
        ax_legend.axis("off")  # Hide axes

        # Create horizontal legend
        ax_legend.legend(legend_handles, dashes.keys(), loc="center", ncol=len(dashes), fontsize=14, frameon=False)

        # Save the legend separately
        fig_legend.savefig('out/legend.png', dpi=300, bbox_inches='tight')

