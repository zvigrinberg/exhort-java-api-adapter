package com.redhat.exhort;

import com.redhat.exhort.image.ImageRef;

public class ImageRefInput extends ImageRef {

  private String originalArgument;

  public ImageRefInput(String originalArgument, String image, String platform) {
    super(image, platform);
    this.originalArgument = originalArgument;
  }

  public String getOriginalArgument() {
    return originalArgument;
  }
}
