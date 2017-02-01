package au.com.dius.pactconsumer.presentation;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import au.com.dius.pactconsumer.R;
import au.com.dius.pactconsumer.data.model.Animal;

public class AnimalsAdapter extends RecyclerView.Adapter<AnimalsAdapter.ViewHolder> {

  private final List<Animal> animals;

  public AnimalsAdapter(@NonNull List<Animal> animals) {
    this.animals = animals;
  }

  @Override
  public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_animal, parent, false));
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    Animal animal = animals.get(position);
    holder.titleView.setText(animal.getName());
    Context context = holder.itemView.getContext();
    int id = holder.itemView.getContext().getResources().getIdentifier(animal.getType(), "drawable", context.getPackageName());
    holder.imageView.setImageDrawable(context.getResources().getDrawable(id, null));
  }

  @Override
  public int getItemCount() {
    return animals.size();
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;
    public TextView titleView;

    public ViewHolder(View itemView) {
      super(itemView);
      imageView = (ImageView) itemView.findViewById(R.id.img_animal);
      titleView = (TextView) itemView.findViewById(R.id.txt_title);
    }

  }

}
